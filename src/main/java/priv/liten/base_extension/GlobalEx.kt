package priv.liten.base_extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import priv.liten.base_data.Arg02
import priv.liten.base_data.Arg03
import priv.liten.base_data.Arg04
import priv.liten.base_data.Arg05


// https://stackoverflow.com/questions/70218354/no-support-for-zipping-multiple-flows-in-kotlin

/**併發型串接*/
inline fun <A:Any?, B> flowZip(
    aFlow: Flow<A>,
    crossinline transfer: (A) -> B
): Flow<B> = aFlow.map { transfer(it) }
/**併發型串接*/
inline fun <A:Any?, B:Any?, C> flowZip(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    crossinline transfer: (A, B) -> C
): Flow<C> = aFlow.zip(bFlow) { a, b -> transfer(a, b) }
/**併發型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D> flowZip(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    crossinline transfer: (A, B, C) -> D
): Flow<D> = aFlow.zip(bFlow, :: Arg02).zip(cFlow) { (a, b), c -> transfer(a, b, c) }
/**併發型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D:Any?, E> flowZip(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    dFlow: Flow<D>,
    crossinline transfer: (A, B, C, D) -> E
): Flow<E> = aFlow.zip(bFlow, :: Arg02).zip(cFlow) { (a, b), c -> Arg03(a, b, c) }.zip(dFlow) { (a, b, c), d -> transfer(a, b, c, d) }
/**併發型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D:Any?, E:Any?, F> flowZip(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    dFlow: Flow<D>,
    eFlow: Flow<E>,
    crossinline transfer: (A, B, C, D, E) -> F
): Flow<F> = aFlow.zip(bFlow, :: Arg02)
    .zip(cFlow) { (a, b), c -> Arg03(a, b, c) }
    .zip(dFlow) { (a, b, c), d -> Arg04(a, b, c, d) }
    .zip(eFlow) { (a, b, c, d), e -> transfer(a, b, c, d, e) }
/**併發型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D:Any?, E:Any?, F:Any?, G> flowZip(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    dFlow: Flow<D>,
    eFlow: Flow<E>,
    fFlow: Flow<F>,
    crossinline transfer: (A, B, C, D, E, F) -> G
): Flow<G> = aFlow.zip(bFlow, :: Arg02)
    .zip(cFlow) { (a, b), c -> Arg03(a, b, c) }
    .zip(dFlow) { (a, b, c), d -> Arg04(a, b, c, d) }
    .zip(eFlow) { (a, b, c, d), e -> Arg05(a, b, c, d, e) }
    .zip(fFlow) { (a, b, c, d, e), f -> transfer(a, b, c, d, e, f) }

/**串接型串接*/
inline fun <A:Any?, B:Any?, C> flowConcat(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    crossinline transfer: (A, B) -> C
): Flow<C> = flow { aFlow.collect { a -> bFlow.collect { b -> emit(transfer(a, b)) } } }
/**串接型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D> flowConcat(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    crossinline transfer: (A, B, C) -> D
): Flow<D> = flow { aFlow.collect { a -> bFlow.collect { b -> cFlow.collect { c -> emit(transfer(a, b, c)) } } } }
/**串接型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D:Any?, E> flowConcat(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    dFlow: Flow<D>,
    crossinline transfer: (A, B, C, D) -> E
): Flow<E> = flow { aFlow.collect { a -> bFlow.collect { b -> cFlow.collect { c -> dFlow.collect { d -> emit(transfer(a, b, c, d)) } } } } }
/**串接型串接*/
inline fun <A:Any?, B:Any?, C:Any?, D:Any?, E:Any?, F> flowConcat(
    aFlow: Flow<A>,
    bFlow: Flow<B>,
    cFlow: Flow<C>,
    dFlow: Flow<D>,
    eFlow: Flow<E>,
    crossinline transfer: (A, B, C, D, E) -> F
): Flow<F> = flow {
    aFlow.collect { a ->
        bFlow.collect { b ->
            cFlow.collect { c ->
                dFlow.collect { d ->
                    eFlow.collect { e ->
                        emit(transfer(a, b, c, d, e)  )
                    }
                }
            }
        }
    }
}